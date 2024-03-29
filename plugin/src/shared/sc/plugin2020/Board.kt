package sc.plugin2020

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamConverter
import com.thoughtworks.xstream.annotations.XStreamImplicit
import com.thoughtworks.xstream.converters.collections.ArrayConverter
import com.thoughtworks.xstream.converters.extended.ToStringConverter
import sc.api.plugins.IBoard
import sc.plugin2020.util.Constants
import sc.plugin2020.util.CubeCoordinates
import sc.shared.PlayerColor
import java.util.*
import kotlin.collections.HashSet
import kotlin.math.max
import kotlin.math.min

@XStreamAlias(value = "board")
data class Board(
        @XStreamConverter(value = ArrayConverter::class, nulls = [ToStringConverter::class])
        @XStreamImplicit(itemFieldName = "fields")
        val gameField: Array<Array<Field?>>
): IBoard {
    
    val fields: List<Field>
        get() = gameField.flatMap { it.filterNotNull() }
    
    constructor(): this(fillGameField()) {
        generateObstructed()
    }
    
    constructor(fields: Collection<Field>): this(gameFieldFromFields(fields))
    
    public override fun clone() = Board(fields)
    
    private fun generateObstructed() {
        val all = this.fields
        val toBeObstructed = HashSet<Field>()
        while(toBeObstructed.size < 3)
            toBeObstructed.add(all.random())
        toBeObstructed.forEach {
            this.gameField[it.x + SHIFT][it.y + SHIFT] = Field(it.x, it.y, it.z, Stack(), true)
        }
    }
    
    fun getField(pos: CubeCoordinates): Field =
            gameField[pos.x + SHIFT][pos.y + SHIFT] ?: throw IndexOutOfBoundsException("No field at $pos")
    
    override fun getField(cubeX: Int, cubeY: Int): Field =
            this.getField(CubeCoordinates(cubeX, cubeY))
    
    fun getField(cubeX: Int, cubeY: Int, cubeZ: Int): Field =
            this.getField(CubeCoordinates(cubeX, cubeY, cubeZ))
    
    /** @return all Pieces on the Board. Prefer [GameState.getDeployedPieces] if possible for better performance. */
    fun getPieces(): List<Piece> {
        val pieces = mutableListOf<Piece>()
        for(x in -SHIFT..SHIFT) {
            for(y in max(-SHIFT, -x - SHIFT)..min(SHIFT, -x + SHIFT)) {
                val field = gameField[x + SHIFT][y + SHIFT]
                if(field != null)
                    pieces.addAll(field.pieces)
            }
        }
        return pieces
    }
    
    override fun toString(): String {
        var text = "Board\n"
        for(x in 0 until Constants.BOARD_SIZE) {
            for(y in 0 until Constants.BOARD_SIZE) {
                val field = this.gameField[x][y]
                if(field == null) {
                    text += "00"
                } else {
                    if(field.hasOwner) {
                        text += (field.owner.toString().get(0) + "T")
                    } else {
                        text += "[]"
                    }
                }
            }
            text += "\n"
        }
        return text
    }
    
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(javaClass != other?.javaClass) return false
        
        other as Board
        
        if(!gameField.contentDeepEquals(other.gameField)) return false
        
        return true
    }
    
    override fun hashCode(): Int =
            gameField.contentDeepHashCode()
    
    fun getFieldsOwnedBy(owner: PlayerColor): List<Field> = fields.filter { it.owner == owner }
    
    companion object {
        private const val SHIFT = (Constants.BOARD_SIZE - 1) / 2
        
        private fun emptyGameField() = Array(Constants.BOARD_SIZE) { arrayOfNulls<Field>(Constants.BOARD_SIZE) }
        
        private fun fillGameField(gameField: Array<Array<Field?>> = emptyGameField()): Array<Array<Field?>> {
            for(x in -SHIFT..SHIFT) {
                for(y in max(-SHIFT, -x - SHIFT)..min(SHIFT, -x + SHIFT)) {
                    if(gameField[x + SHIFT][y + SHIFT] == null) {
                        gameField[x + SHIFT][y + SHIFT] = Field(CubeCoordinates(x, y))
                    }
                }
            }
            return gameField
        }
        
        private fun gameFieldFromFields(fields: Collection<Field>): Array<Array<Field?>> {
            val gameField = emptyGameField()
            var x: Int
            var y: Int
            for(f in fields) {
                if(f.coordinates.x > SHIFT || f.coordinates.x < -SHIFT || f.coordinates.y > SHIFT || f.coordinates.y < -SHIFT)
                    throw IndexOutOfBoundsException()
                x = f.coordinates.x + SHIFT
                y = f.coordinates.y + SHIFT
                gameField[x][y] = f
            }
            return gameField
        }
    }
}